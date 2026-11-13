package org.delcom.ifs25042springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/**
 * Menyimpan sesi login di VaadinSession, HTTP session, dan cookie
 * agar tetap ada setelah refresh browser.
 */
public final class AuthSession {

    private static final String USER_KEY = "currentUser";
    private static final String USER_ID_KEY = "currentUserId";
    private static final String LOGGED_OUT_KEY = "authLoggedOut";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 7;

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        session.setAttribute(USER_KEY, user);
        session.setAttribute(USER_ID_KEY, user.getId());
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            httpSession.setAttribute(USER_ID_KEY, user.getId());
            httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        }
        writeUserCookie(user.getId());
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(USER_KEY, null);
            session.setAttribute(USER_ID_KEY, null);
            session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            WrappedSession httpSession = session.getSession();
            if (httpSession != null) {
                httpSession.setAttribute(USER_ID_KEY, null);
                httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            }
        }
        clearUserCookie();
    }

    public static Optional<User> currentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || isLoggedOut(session)) {
            return Optional.empty();
        }
        Object value = session.getAttribute(USER_KEY);
        if (value instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Mengambil user login; jika hilang karena refresh, pulihkan dari session/cookie + database.
     */
    public static Optional<User> currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && isLoggedOut(session)) {
            return Optional.empty();
        }
        Optional<User> existing = currentUser();
        if (existing.isPresent()) {
            return existing;
        }
        return restore(userRepository);
    }

    public static void refresh(User user) {
        login(user);
    }

    private static boolean isLoggedOut(VaadinSession session) {
        Object flag = session.getAttribute(LOGGED_OUT_KEY);
        if (Boolean.TRUE.equals(flag)) {
            return true;
        }
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            return Boolean.TRUE.equals(httpSession.getAttribute(LOGGED_OUT_KEY));
        }
        return false;
    }

    private static Optional<User> restore(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && isLoggedOut(session)) {
            return Optional.empty();
        }
        Integer userId = readStoredUserId();
        if (userId == null) {
            return Optional.empty();
        }
        Optional<User> found = userRepository.findById(userId);
        found.ifPresent(AuthSession::login);
        if (found.isEmpty()) {
            clearUserCookie();
        }
        return found;
    }

    private static Integer readStoredUserId() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            if (isLoggedOut(session)) {
                return null;
            }
            Object fromVaadin = session.getAttribute(USER_ID_KEY);
            if (fromVaadin instanceof Integer id) {
                return id;
            }
            WrappedSession httpSession = session.getSession();
            if (httpSession != null) {
                Object fromHttp = httpSession.getAttribute(USER_ID_KEY);
                if (fromHttp instanceof Integer id) {
                    return id;
                }
            }
        }
        return readUserIdFromCookie();
    }

    private static void writeUserCookie(int userId) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, String.valueOf(userId));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }

    private static void clearUserCookie() {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private static Integer readUserIdFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                try {
                    return Integer.parseInt(cookie.getValue());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
