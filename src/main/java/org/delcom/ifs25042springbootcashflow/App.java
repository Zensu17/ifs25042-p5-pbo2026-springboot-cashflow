package org.delcom.ifs25042springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.PWA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet("styles.css")
@PageTitle("Cashflow")
@Meta(name = "description", content = "Kelola pemasukan, pengeluaran, dan saldo cashflow Anda.")
@Meta(name = "robots", content = "index, follow")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
public class App implements AppShellConfigurator {
    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
