package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global untuk tindakan yang memerlukan waktu tunggu.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);

            uiEvent.getUI().getPage().executeJs(
                    "const hideIndicator = () => {" +
                    "  const el = document.querySelector('vaadin-connection-indicator');" +
                    "  if (el) {" +
                    "    el.setAttribute('aria-hidden', 'true');" +
                    "    return true;" +
                    "  }" +
                    "  return false;" +
                    "};" +
                    "if (!hideIndicator()) {" +
                    "  const obs = new MutationObserver(() => { if (hideIndicator()) obs.disconnect(); });" +
                    "  obs.observe(document.body, { childList: true });" +
                    "}"
            );
        });
    }
}
