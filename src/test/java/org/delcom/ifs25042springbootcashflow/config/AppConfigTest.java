package org.delcom.ifs25042springbootcashflow.config;

import org.delcom.ifs25042springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25042springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25042springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25042springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25042springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25042springbootcashflow.usecase.UserUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresBeans() throws Exception {
        AppConfig config = new AppConfig();
        DataSource dataSource = createDataSource();

        IUserRepository userRepository = config.userRepository(dataSource);
        ITransactionRepository transactionRepository = config.transactionRepository(dataSource);
        FinanceUseCase financeUseCase = config.financeUseCase(transactionRepository);
        AuthUseCase authUseCase = config.authUseCase(userRepository);
        UserUseCase userUseCase = config.userUseCase(userRepository);
        FinancePresenter presenter = config.financePresenter();
        FileStorage fileStorage = config.fileStorage(tempDir.toString());

        assertNotNull(userRepository);
        assertNotNull(transactionRepository);
        assertNotNull(financeUseCase);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
        assertInstanceOf(ITransactionRepository.class, transactionRepository);

        config.seedRootAdmin(authUseCase, "admin", "admin")
                .run(new DefaultApplicationArguments());

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        var registration = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.uploadResourceConfigurer(fileStorage).addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
    }

    @Test
    void cachesStaticAssets() throws Exception {
        AppConfig config = new AppConfig();
        var filter = config.staticAssetCacheFilter().getFilter();

        MockHttpServletResponse cachedResponse = new MockHttpServletResponse();
        filter.doFilter(
                new MockHttpServletRequest("GET", "/VAADIN/build/app.js"),
                cachedResponse,
                (req, res) -> {
                    jakarta.servlet.http.HttpServletResponse httpRes = (jakarta.servlet.http.HttpServletResponse) res;
                    httpRes.setHeader("Cache-Control", "no-cache");
                    httpRes.addHeader("Cache-Control", "no-store");
                    httpRes.setHeader("Content-Type", "application/javascript");
                    httpRes.addHeader("X-Custom", "test");
                });

        assertEquals(
                "public, max-age=31536000, immutable",
                cachedResponse.getHeader("Cache-Control"));
        assertEquals("application/javascript", cachedResponse.getHeader("Content-Type"));
        assertEquals("test", cachedResponse.getHeader("X-Custom"));

        MockHttpServletResponse uncachedResponse = new MockHttpServletResponse();
        filter.doFilter(
                new MockHttpServletRequest("GET", "/login"),
                uncachedResponse,
                new MockFilterChain());

        assertNull(uncachedResponse.getHeader("Cache-Control"));

        assertTrue(config.isStaticAsset("/VAADIN/build/app.js"));
        assertTrue(config.isStaticAsset("/lumo/lumo.css"));
        assertTrue(config.isStaticAsset("/icons/icon.png"));
        assertTrue(config.isStaticAsset("/styles.css"));
        assertTrue(config.isStaticAsset("/test.js"));
        assertTrue(config.isStaticAsset("/image.png"));
        assertTrue(config.isStaticAsset("/favicon.ico"));
        assertFalse(config.isStaticAsset("/login"));
        assertFalse(config.isStaticAsset(null));
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:appconfig_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
