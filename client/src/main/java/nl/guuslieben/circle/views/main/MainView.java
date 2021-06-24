package nl.guuslieben.circle.views.main;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.common.message.Message;
import nl.guuslieben.circle.common.message.MessageUtilities;
import nl.guuslieben.circle.views.MainLayout;

/**
 * A Designer generated component for the stub-tag template.
 *
 * Designer will add and remove fields with @Id mappings but does not overwrite
 * or otherwise change this file.
 */
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("The Circle")
@Tag("main-view")
@JsModule("./views/main-view.ts")
public class MainView extends LitTemplate {

    @Id
    private Button sayHello;

    private final ClientService service;

    public MainView(ClientService service) {
        this.service = service;

        this.sayHello.addClickListener(e -> {
            final Optional<Message> result = this.service.request("http://localhost:9090/");
            if (result.isPresent()) {
                final Message message = result.get();
                if (MessageUtilities.verify(message)) {
                    Notification.show("Received a validated message at " + message.getTimestamp());
                } else {
                    Notification.show("Received a invalid message at " + message.getTimestamp());
                }
            }
        });
    }
}
