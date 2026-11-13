pipeline {

    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
        // Stop later stages if a publisher marks the build UNSTABLE/FAILURE mid-run
        skipStagesAfterUnstable()
    }

    environment {
        MAVEN_OPTS = '-Xmx2g'
        JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
        // Cache Maven bersama di volume Jenkins, persist antar build/job (hindari 429)
        MAVEN_LOCAL_REPO = '/var/jenkins_home/.m2/repository'
    }

    stages {

        // ============================================================
        // CHECKOUT
        // ============================================================
        stage('Checkout') {
            agent {
                docker {
                    image 'alpine:3.21'
                    reuseNode true
                }
            }

            steps {
                checkout scm
            }
        }

        // ============================================================
        // BUILD & TEST (Maven + Jacoco 100%)
        // ============================================================
        stage('Build & Test') {
            agent {
                docker {
                    image 'eclipse-temurin:25-jdk'
                    reuseNode true
                    args '-u root -v /var/jenkins_home/.m2:/var/jenkins_home/.m2'
                }
            }

            steps {
                sh '''
                    set -e

                    echo "=== Java Runtime ==="
                    java -version

                    chmod +x mvnw
                    mkdir -p "$MAVEN_LOCAL_REPO"

                    if [ ! -f .env ]; then
                        cp .env.example .env
                    fi

                    echo "=== Maven Clean Verify (Jacoco 100%) ==="
                    # verify = test + jacoco:check (instruction/branch/line/method/class = 100%)
                    # -Pproduction = Vaadin frontend production build saat package
                    # cache Maven persisten agar tidak download ulang (hindari 429 Maven Central)
                    ./mvnw -B clean verify -Pproduction \
                        -Dmaven.repo.local="$MAVEN_LOCAL_REPO"

                    echo "=== Build & Tests Passed ==="
                    ls -lh target/*.jar || true
                    ls -lh target/site/jacoco/jacoco.xml || true
                '''
            }

            post {
                always {
                    junit(
                        allowEmptyResults: true,
                        testResults: 'target/surefire-reports/*.xml'
                    )
                    archiveArtifacts(
                        artifacts: 'target/site/jacoco/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        // ============================================================
        // TRIVY SECURITY SCAN
        // ============================================================
        stage('Trivy Security Scan') {
            agent {
                docker {
                    image 'aquasec/trivy:0.76.1'
                    reuseNode true

                    args '''
                        --entrypoint=""
                        -e HOME=/tmp
                        -e XDG_CACHE_HOME=/tmp/.cache
                        -v /var/jenkins_home/.m2:/var/jenkins_home/.m2
                    '''
                }
            }

            steps {
                sh '''
                    set -e

                    mkdir -p .trivy-cache || true

                    # Trivy Java analyzer membaca ~/.m2; HOME container = /tmp.
                    # Arahkan ke cache Maven bersama yang persist di volume Jenkins.
                    mkdir -p /tmp/.m2
                    ln -sfn "$MAVEN_LOCAL_REPO" /tmp/.m2/repository

                    echo "======================================"
                    echo "        TRIVY SECURITY SCAN"
                    echo "======================================"

                    echo "=== Trivy Version ==="
                    trivy --version

                    echo "=== Local Maven cache ==="
                    ls -ld "$MAVEN_LOCAL_REPO" /tmp/.m2/repository || true

                    echo "=== Trivy Scan ==="

                    trivy fs \
                        --cache-dir .trivy-cache \
                        --scanners vuln \
                        --severity HIGH,CRITICAL \
                        --offline-scan \
                        --skip-dirs target \
                        --skip-dirs .mvn \
                        --skip-dirs node_modules \
                        --skip-dirs .trivy-cache \
                        --format sarif \
                        --output trivy-results.sarif \
                        --exit-code 1 \
                        .

                    echo "=== Trivy Result ==="
                    ls -lh trivy-results.sarif
                '''
            }

            post {
                always {
                    // failOnError must be false: otherwise Warnings NG can mark the
                    // whole build FAILURE while later stages still run (all green, badge red).
                    // Build failure on HIGH/CRITICAL comes from trivy --exit-code 1 above.
                    recordIssues(
                        enabledForFailure: true,
                        failOnError: false,
                        tools: [
                            sarif(
                                id: 'trivy',
                                name: 'Trivy Security',
                                pattern: 'trivy-results.sarif'
                            )
                        ]
                    )
                }
            }
        }

        // ============================================================
        // PACKAGE APPLICATION (Spring Boot JAR)
        // ============================================================
        stage('Package Application') {
            agent {
                docker {
                    image 'eclipse-temurin:25-jdk'
                    reuseNode true
                    args '-u root'
                }
            }

            steps {
                sh '''
                    set -e

                    echo "======================================"
                    echo "       CREATING APPLICATION PACKAGE"
                    echo "======================================"

                    apt-get update -qq
                    apt-get install -y -qq zip unzip git >/dev/null

                    rm -f latest-app.zip app.jar

                    # Ambil fat JAR Spring Boot (abaikan *.jar.original)
                    JAR="$(ls -1 target/*-SNAPSHOT.jar 2>/dev/null | grep -v '\\.original$' | head -n 1 || true)"
                    if [ -z "$JAR" ] || [ ! -f "$JAR" ]; then
                        echo "ERROR: Spring Boot JAR tidak ditemukan di target/"
                        ls -la target || true
                        exit 1
                    fi

                    echo "Using JAR: $JAR"
                    cp "$JAR" app.jar

                    # ZIP bersih seperti hasil clone GitHub (hanya file tracked),
                    # lalu tambahkan JAR hasil build di target/ agar deploy zip
                    # berperilaku sama dengan deploy git.
                    git -c safe.directory="$PWD" archive --format=zip --output=latest-app.zip HEAD
                    zip -q latest-app.zip "$JAR"

                    echo "=== Application Package Created ==="
                    ls -lh latest-app.zip app.jar

                    echo "=== Package Content ==="
                    unzip -l latest-app.zip
                '''
            }
        }

        // ============================================================
        // PUBLISH APPLICATION
        // ============================================================
        stage('Publish Application') {

            steps {

                // ========================================================
                // 1. ARCHIVE ARTIFACT KE JENKINS
                // ========================================================

                archiveArtifacts(
                    artifacts: 'latest-app.zip,app.jar',
                    fingerprint: true,
                    allowEmptyArchive: false
                )

                script {

                    // ====================================================
                    // 2. BUAT IDENTITAS APPLICATION
                    // ====================================================

                    def appName = env.JOB_NAME
                        .replaceAll('[^a-zA-Z0-9._-]', '-')
                        .replaceAll('-+', '-')
                        .replaceAll('^-|-$', '')

                    def buildId = env.BUILD_NUMBER

                    echo "Application Name: ${appName}"
                    echo "Build ID: ${buildId}"

                    // ====================================================
                    // 3. COPY KE USER CONTENT
                    // ====================================================

                    sh """
                        set -e

                        echo "======================================"
                        echo "       PUBLISHING USER CONTENT"
                        echo "======================================"

                        docker exec cicd-jenkins \\
                            mkdir -p \\
                            "/var/jenkins_home/userContent/applications/${appName}/${buildId}"

                        docker cp \\
                            latest-app.zip \\
                            "cicd-jenkins:/var/jenkins_home/userContent/applications/${appName}/${buildId}/latest-app.zip"

                        echo "=== Published File ==="

                        docker exec cicd-jenkins \\
                            ls -lh \\
                            "/var/jenkins_home/userContent/applications/${appName}/${buildId}/latest-app.zip"
                    """

                    // ====================================================
                    // 4. BUAT PUBLIC ARTIFACT URL
                    // ====================================================

                    def jenkinsBaseUrl = env.BUILD_URL
                        .substring(0, env.BUILD_URL.indexOf('/job/'))
                        .replace('localhost', 'host.docker.internal')

                    env.ARTIFACT_URL =
                        "${jenkinsBaseUrl}/userContent/applications/${appName}/${buildId}/latest-app.zip"

                    echo "======================================"
                    echo "       APPLICATION PUBLISHED"
                    echo "======================================"

                    echo "Artifact URL:"
                    echo "${env.ARTIFACT_URL}"
                }
            }
        }

        // ============================================================
        // DEPLOY APPLICATION
        // ============================================================
        stage('Deploy Application') {
            agent {
                docker {
                    image 'curlimages/curl:8.15.0'
                    reuseNode true
                    args '--network cicd-network'
                }
            }

            steps {
                script {

                    echo "=========================================="
                    echo "       START APPLICATION DEPLOYMENT"
                    echo "=========================================="

                    echo "Artifact URL:"
                    echo "${env.ARTIFACT_URL}"

                    // ==================================================
                    // 1. REQUEST REDEPLOYMENT
                    // ==================================================

                    echo ""
                    echo "=== Request Redeployment ==="

                    def redeployResponse = sh(
                        script: '''
                            set -e

                            curl -sS --fail-with-body \
                                -X POST "$URL_REDEPLOY" \
                                -H "Content-Type: application/json" \
                                -d "{
                                    \\"token_access\\": \\"$DEPLOY_TOKEN\\",
                                    \\"website_id\\": \\"$WEBSITE_ID\\",
                                    \\"source_url\\": \\"$ARTIFACT_URL\\",
                                    \\"source_type\\": \\"jenkins\\"
                                }"
                        ''',
                        returnStdout: true
                    ).trim()

                    echo "Redeploy Response:"
                    echo redeployResponse

                    // ==================================================
                    // 2. POLLING DEPLOYMENT PROGRESS
                    // ==================================================

                    echo ""
                    echo "=== Waiting For Deployment ==="

                    def maxAttempts = 120
                    def attempt = 0
                    def deploymentStatus = 'IN_PROGRESS'

                    while (deploymentStatus == 'IN_PROGRESS') {

                        attempt++

                        if (attempt > maxAttempts) {
                            error(
                                "Deployment timeout. " +
                                "Status masih IN_PROGRESS setelah " +
                                "${maxAttempts} attempts."
                            )
                        }

                        sleep time: 5, unit: 'SECONDS'

                        echo ""
                        echo "=== Checking Deployment Progress (${attempt}/${maxAttempts}) ==="

                        def progressResponse = sh(
                            script: '''
                                set -e

                                curl -sS --fail-with-body \
                                    -X POST "$URL_PROGRESS" \
                                    -H "Content-Type: application/json" \
                                    -d "{
                                        \\"token_access\\": \\"$DEPLOY_TOKEN\\",
                                        \\"website_id\\": \\"$WEBSITE_ID\\"
                                    }"
                            ''',
                            returnStdout: true
                        ).trim()

                        echo "Progress Response:"
                        echo progressResponse

                        // ==================================================
                        // PARSE JSON
                        // ==================================================

                        def json = readJSON text: progressResponse

                        deploymentStatus = json?.data?.status
                            ?.toString()
                            ?.toUpperCase()

                        if (!deploymentStatus) {
                            error(
                                "Response progress tidak memiliki data.status"
                            )
                        }

                        echo "Deployment Status: ${deploymentStatus}"

                        // ==================================================
                        // SUCCESS
                        // ==================================================

                        if (deploymentStatus == 'SUCCESS') {

                            echo ""
                            echo "=========================================="
                            echo "       ✅ DEPLOYMENT SUCCESS"
                            echo "=========================================="

                            break
                        }

                        // ==================================================
                        // FAIL
                        // ==================================================

                        if (deploymentStatus == 'FAIL') {

                            echo ""
                            echo "=========================================="
                            echo "       ❌ DEPLOYMENT FAILED"
                            echo "=========================================="

                            def deploymentLog =
                                json?.data?.log
                                    ?: 'Deployment failed tanpa log.'

                            echo ""
                            echo "========== DEPLOYMENT LOG =========="
                            echo deploymentLog
                            echo "===================================="

                            error(
                                "Deployment gagal untuk website " +
                                "${WEBSITE_ID}"
                            )
                        }

                        // ==================================================
                        // OTHER STATUS
                        // ==================================================

                        echo "Deployment masih berjalan..."
                    }

                    echo ""
                    echo "=========================================="
                    echo "       DEPLOYMENT FINISHED"
                    echo "=========================================="
                }
            }
        }
    }

    // ================================================================
    // POST ACTIONS
    // ================================================================
    post {

        always {
            archiveArtifacts(
                artifacts: 'trivy-results.sarif',
                allowEmptyArchive: true
            )
        }

        success {
            echo "=========================================="
            echo "       ✅ PIPELINE SUCCESS"
            echo "=========================================="
            echo "Result: ${currentBuild.currentResult}"
            echo "📦 Artifact: ${env.ARTIFACT_URL ?: '(not published)'}"
            echo "🚀 Aplikasi Spring Boot berhasil dideploy."
        }

        failure {
            echo "=========================================="
            echo "       ❌ PIPELINE FAILED"
            echo "=========================================="
            echo "Result: ${currentBuild.currentResult}"
            echo "Periksa log stage yang merah / Console Output untuk penyebab gagal."
        }

        unstable {
            echo "=========================================="
            echo "       ⚠️ PIPELINE UNSTABLE"
            echo "=========================================="
            echo "Result: ${currentBuild.currentResult}"
        }
    }
}