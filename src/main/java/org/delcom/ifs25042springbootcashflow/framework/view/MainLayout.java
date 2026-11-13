package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25042springbootcashflow.framework.auth.AuthSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout utama untuk halaman Cashflow yang memerlukan login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final IUserRepository userRepository;
    private final List<NavEntry> navEntries = new ArrayList<>();
    private final Image headerAvatarImage = new Image();
    private final Span headerAvatarInitial = new Span();
    private final Div headerAvatar = new Div();

    private record NavEntry(RouterLink link, String pathPrefix) {
    }

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        setPrimarySection(Section.DRAWER);
        AuthSession.currentUser(userRepository);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        HorizontalLayout brandTitle = appBrandTitle("1.2rem", "28px");
        configureHeaderAvatar();
        refreshHeaderAvatar(AuthSession.currentUser().orElse(null));

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.getElement().setAttribute("aria-label", "Buka atau tutup navigasi");

        HorizontalLayout header = new HorizontalLayout(drawerToggle, brandTitle, headerAvatar);
        header.getElement().setAttribute("role", "banner");
        header.getElement().setAttribute("aria-label", "Header Aplikasi");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(brandTitle);
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "#FFFFFF")
                .set("border-bottom", "1px solid #E5E7EB");
        addToNavbar(header);
    }

    private HorizontalLayout appBrandTitle(String fontSize, String iconSize) {
        Image logo = new Image("icons/icon.png", "Cashflow");
        logo.setWidth(iconSize);
        logo.setHeight(iconSize);
        logo.getStyle()
                .set("display", "block")
                .set("border-radius", "6px")
                .set("object-fit", "cover")
                .set("flex-shrink", "0");

        Span title = new Span("Cashflow");
        title.getStyle()
                .set("font-size", fontSize)
                .set("font-weight", "700")
                .set("margin", "0")
                .set("color", "#111827")
                .set("line-height", "1.2");

        HorizontalLayout brand = new HorizontalLayout(logo, title);
        brand.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);
        brand.setPadding(false);
        brand.getStyle().set("gap", "0.65rem");
        return brand;
    }

    private void configureHeaderAvatar() {
        headerAvatarImage.setWidth("40px");
        headerAvatarImage.setHeight("40px");
        headerAvatarImage.getStyle()
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("display", "block");

        headerAvatarInitial.getStyle()
                .set("width", "40px")
                .set("height", "40px")
                .set("border-radius", "50%")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background", "#DBEAFE")
                .set("color", "#1D4ED8")
                .set("font-weight", "700")
                .set("font-size", "0.95rem");

        headerAvatar.removeAll();
        headerAvatar.add(headerAvatarInitial);
        headerAvatar.getStyle()
                .set("cursor", "pointer")
                .set("border", "2px solid #93C5FD")
                .set("border-radius", "50%")
                .set("overflow", "hidden")
                .set("width", "40px")
                .set("height", "40px")
                .set("flex-shrink", "0");
        headerAvatar.getElement().setAttribute("title", "Menu akun");

        ContextMenu accountMenu = new ContextMenu(headerAvatar);
        accountMenu.setOpenOnClick(true);
        AuthSession.currentUser().ifPresent(user -> {
            var identityItem = accountMenu.addItem(accountIdentity(user));
            identityItem.setEnabled(false);
            identityItem.getStyle().set("cursor", "default");

            var profileItem = accountMenu.addItem(menuItemContent(VaadinIcon.USER, "Profil", "#4B5563", "#1F2937"),
                    event -> UI.getCurrent().navigate(ProfileView.class));
            profileItem.getStyle().set("cursor", "pointer");

            var logoutItem = accountMenu.addItem(menuItemContent(VaadinIcon.SIGN_OUT, "Keluar", "#DC2626", "#DC2626"),
                    event -> {
                        AuthSession.logout();
                        UI.getCurrent().getPage().setLocation("login");
                    });
            logoutItem.getStyle().set("cursor", "pointer");
        });
    }

    private VerticalLayout accountIdentity(User user) {
        Span username = new Span(user.getUsername());
        username.getStyle()
                .set("display", "block")
                .set("font-weight", "700")
                .set("font-size", "0.95rem")
                .set("color", "#111827")
                .set("margin", "0");

        Span role = new Span(user.getRole().name());
        role.getStyle()
                .set("display", "block")
                .set("margin-top", "0.15rem")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("letter-spacing", "0.02em")
                .set("color", "#1D4ED8");

        VerticalLayout identity = new VerticalLayout(username, role);
        identity.setPadding(false);
        identity.setSpacing(false);
        identity.setMargin(false);
        identity.getStyle()
                .set("gap", "0")
                .set("padding", "0.15rem 0")
                .set("min-width", "8rem");
        return identity;
    }

    private HorizontalLayout menuItemContent(VaadinIcon iconType, String label, String iconColor, String textColor) {
        Icon icon = iconType.create();
        icon.setSize("1rem");
        icon.getStyle()
                .set("color", iconColor)
                .set("flex-shrink", "0")
                .set("margin", "0");

        Span text = new Span(label);
        text.getStyle()
                .set("color", textColor)
                .set("font-weight", "600")
                .set("margin", "0");

        HorizontalLayout content = new HorizontalLayout(icon, text);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        content.setSpacing(false);
        content.setPadding(false);
        content.setMargin(false);
        content.setWidth(null);
        content.getStyle()
                .set("gap", "0.5rem")
                .set("margin", "0")
                .set("padding", "0")
                .set("justify-content", "flex-start")
                .set("display", "inline-flex");
        return content;
    }

    private void refreshHeaderAvatar(User user) {
        headerAvatar.removeAll();
        if (user == null) {
            headerAvatarInitial.setText("?");
            headerAvatar.add(headerAvatarInitial);
            return;
        }
        String initial = user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername().substring(0, 1).toUpperCase()
                : user.getFullName().substring(0, 1).toUpperCase();
        headerAvatarInitial.setText(initial);

        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            headerAvatarImage.setSrc("/uploads/" + user.getPhotoPath() + "?t=" + System.currentTimeMillis());
            headerAvatarImage.setAlt(user.getFullName());
            headerAvatar.add(headerAvatarImage);
        } else {
            headerAvatar.add(headerAvatarInitial);
        }
    }

    private void createDrawer() {
        HorizontalLayout drawerBrand = appBrandTitle("1.15rem", "30px");
        Div brandHeader = new Div(drawerBrand);
        brandHeader.setWidthFull();
        brandHeader.getStyle()
                .set("padding", "1.1rem 1rem")
                .set("border-bottom", "1px solid #E5E7EB")
                .set("box-sizing", "border-box");

        VerticalLayout menuList = new VerticalLayout();
        menuList.setPadding(true);
        menuList.setSpacing(false);
        menuList.setWidthFull();
        menuList.getStyle().set("gap", "0.35rem");

        menuList.add(createNavItem("Cashflow Saya", VaadinIcon.WALLET, FinanceView.class, ""));
        menuList.add(createNavItem("Profil", VaadinIcon.USER, ProfileView.class, "profile"));

        AuthSession.currentUser().ifPresent(user -> {
            if (user.isAdmin()) {
                menuList.add(createNavItem("Daftar Pengguna", VaadinIcon.USERS, UsersView.class, "users"));
            }
        });

        VerticalLayout drawer = new VerticalLayout(brandHeader, menuList);
        drawer.getElement().setAttribute("role", "navigation");
        drawer.getElement().setAttribute("aria-label", "Menu Navigasi");
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.setSizeFull();
        drawer.getStyle()
                .set("background", "#FFFFFF")
                .set("border-right", "1px solid #E5E7EB");
        addToDrawer(drawer);
    }

    private RouterLink createNavItem(
            String label,
            VaadinIcon iconType,
            Class<? extends Component> view,
            String pathPrefix) {
        Icon icon = iconType.create();
        icon.setSize("1.05rem");
        icon.getStyle().set("color", "#4B5563");

        Span text = new Span(label);
        text.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "500")
                .set("color", "#1F2937");

        HorizontalLayout content = new HorizontalLayout(icon, text);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(false);
        content.getStyle().set("gap", "0.65rem");

        RouterLink link = new RouterLink();
        link.setRoute(view);
        link.add(content);
        link.getStyle()
                .set("display", "block")
                .set("width", "100%")
                .set("text-decoration", "none")
                .set("padding", "0.7rem 0.85rem")
                .set("border-radius", "10px")
                .set("box-sizing", "border-box")
                .set("border", "1px solid transparent")
                .set("transition", "background 0.15s ease")
                .set("cursor", "pointer");

        link.getElement().addEventListener("mouseover", e -> {
            if (!"true".equals(link.getElement().getAttribute("data-active"))) {
                link.getStyle().set("background", "#F3F4F6");
            }
        });
        link.getElement().addEventListener("mouseout", e -> {
            if (!"true".equals(link.getElement().getAttribute("data-active"))) {
                link.getStyle().set("background", "transparent");
            }
        });

        navEntries.add(new NavEntry(link, pathPrefix));
        return link;
    }

    private void highlightActive(String path) {
        String normalized = path == null ? "" : path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        for (NavEntry entry : navEntries) {
            boolean active;
            if (entry.pathPrefix().isEmpty()) {
                active = normalized.isEmpty();
            } else {
                active = normalized.equals(entry.pathPrefix())
                        || normalized.startsWith(entry.pathPrefix() + "/");
            }
            if ("users".equals(entry.pathPrefix()) && normalized.startsWith("user-transactions")) {
                active = true;
            }

            RouterLink link = entry.link();
            if (active) {
                link.getElement().setAttribute("data-active", "true");
                link.getStyle()
                        .set("background", "#DBEAFE")
                        .set("border", "1px solid #93C5FD");
                styleNavChildren(link, "#1D4ED8", "#1E40AF", "600");
            } else {
                link.getElement().setAttribute("data-active", "false");
                link.getStyle()
                        .set("background", "transparent")
                        .set("border", "1px solid transparent");
                styleNavChildren(link, "#4B5563", "#1F2937", "500");
            }
        }
    }

    private void styleNavChildren(RouterLink link, String iconColor, String textColor, String weight) {
        link.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .findFirst()
                .ifPresent(row -> row.getChildren().forEach(child -> {
                    if (child instanceof Icon icon) {
                        icon.getStyle().set("color", iconColor);
                    }
                    if (child instanceof Span span) {
                        span.getStyle().set("color", textColor).set("font-weight", weight);
                    }
                }));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        highlightActive(event.getLocation().getPath());
        refreshHeaderAvatar(AuthSession.currentUser(userRepository).orElse(null));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isEmpty()) {
            event.forwardTo(LoginView.class);
        }
    }
}
