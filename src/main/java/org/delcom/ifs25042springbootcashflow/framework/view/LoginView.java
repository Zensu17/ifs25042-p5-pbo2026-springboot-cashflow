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

@Route("login")
@PageTitle("Login - Cashflow")
public class LoginView extends VerticalLayout {

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            UI.getCurrent().navigate(FinanceView.class);
            return;
        }
        setSizeFull();
        getElement().setAttribute("role", "main");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        setInnerInputId(username, "login-username");
        setInnerInputId(password, "login-password");
        username.setWidthFull();
        password.setWidthFull();
        username.setValueChangeMode(ValueChangeMode.EAGER);
        password.setValueChangeMode(ValueChangeMode.EAGER);

        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setWidthFull();

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");

        Runnable submit = () -> {
            loadingBar.setVisible(true);
            loginButton.setEnabled(false);
            try {
                User user = authUseCase.login(username.getValue(), password.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException ex) {
                loadingBar.setVisible(false);
                loginButton.setEnabled(true);
                Notification n = Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        loginButton.addClickListener(event -> submit.run());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle().set("margin-top", "0.85rem");
        loginButton.getStyle().set("cursor", "pointer");

        username.addKeyUpListener(Key.ENTER, event -> submit.run());
        password.addKeyUpListener(Key.ENTER, event -> submit.run());

        H1 title = new H1("Masuk");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Gunakan kredensial akun kamu untuk mengakses Cashflow");
        subtitle.getStyle().set("margin", "0 0 0.15rem 0").set("color", "#4B5563");

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar", RegisterView.class);
        registerLink.getStyle()
                .set("align-self", "flex-end")
                .set("text-align", "right")
                .set("cursor", "pointer");

        VerticalLayout form = new VerticalLayout(
                title,
                subtitle,
                username,
                password,
                loadingBar,
                loginButton,
                registerLink);
        form.setPadding(false);
        form.setSpacing(false);
        form.setWidthFull();
        form.getStyle().set("gap", "0.45rem");
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

    private void setInnerInputId(com.vaadin.flow.component.Component component, String id) {
        Runnable apply = () -> component.getElement().executeJs(
                "const applyId = () => {" +
                "  const input = this.inputElement || this.querySelector('input');" +
                "  if (input) {" +
                "    input.id = $0;" +
                "    const label = this.querySelector('label');" +
                "    if (label) label.setAttribute('for', $0);" +
                "    return true;" +
                "  }" +
                "  return false;" +
                "};" +
                "if (!applyId()) {" +
                "  if (this.updateComplete) { this.updateComplete.then(applyId); }" +
                "  const obs = new MutationObserver(() => { if (applyId()) obs.disconnect(); });" +
                "  obs.observe(this, { childList: true, subtree: true });" +
                "}", id);
        apply.run();
        component.addAttachListener(event -> apply.run());
    }
}
