package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25042springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25042springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Registrasi - Cashflow")
public class RegisterView extends VerticalLayout {

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            UI.getCurrent().navigate(FinanceView.class);
            return;
        }
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        TextField fullName = new TextField("Nama Lengkap");
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        fullName.setValueChangeMode(ValueChangeMode.EAGER);
        username.setValueChangeMode(ValueChangeMode.EAGER);
        password.setValueChangeMode(ValueChangeMode.EAGER);

        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setWidthFull();

        Button registerButton = new Button("Daftar");

        Runnable submit = () -> {
            loadingBar.setVisible(true);
            registerButton.setEnabled(false);
            try {
                User user = authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException ex) {
                loadingBar.setVisible(false);
                registerButton.setEnabled(true);
                Notification n = Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        registerButton.addClickListener(event -> submit.run());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.getStyle().set("margin-top", "0.85rem");
        registerButton.getStyle().set("cursor", "pointer");

        fullName.addKeyUpListener(Key.ENTER, event -> submit.run());
        username.addKeyUpListener(Key.ENTER, event -> submit.run());
        password.addKeyUpListener(Key.ENTER, event -> submit.run());

        H1 title = new H1("Registrasi");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Buat akun baru untuk mencatat pemasukan dan pengeluaran.");
        subtitle.getStyle().set("margin", "0 0 0.15rem 0").set("color", "#4B5563");

        RouterLink loginLink = new RouterLink("Sudah punya akun? Masuk", LoginView.class);
        loginLink.getStyle()
                .set("align-self", "flex-end")
                .set("text-align", "right")
                .set("cursor", "pointer");

        VerticalLayout form = new VerticalLayout(
                title,
                subtitle,
                fullName,
                username,
                password,
                loadingBar,
                registerButton,
                loginLink);
        form.setPadding(false);
        form.setSpacing(false);
        form.setWidthFull();
        form.getStyle().set("gap", "0.45rem");
        fullName.getStyle().set("margin", "0");
        username.getStyle().set("margin", "0");
        password.getStyle().set("margin", "0");

        Div card = new Div(form);
        card.getStyle()
                .set("width", "100%")
                .set("max-width", "420px")
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("padding", "1.35rem 1.35rem")
                .set("box-shadow", "0 8px 24px rgba(15, 23, 42, 0.08)")
                .set("box-sizing", "border-box");

        add(card);
    }
}
