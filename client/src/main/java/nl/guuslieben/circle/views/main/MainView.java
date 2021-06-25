package nl.guuslieben.circle.views.main;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.common.UserData;
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

    private final transient ClientService service;

    @Id
    private TextField name;
    @Id
    private TextField email;
    @Id
    private PasswordField password;

    @Id
    private Button sayHello;

    @Id
    private Paragraph key;

    public MainView(ClientService service) {
        this.service = service;
        this.sayHello.addClickListener(this::onClick);
    }

    public void onClick(ClickEvent<Button> event) {
        final UserData data = new UserData(this.name.getValue(), this.email.getValue(), -1);
        final boolean validServerCertificate = this.service.csr(data, this.password.getValue());

        if (validServerCertificate) {
            Notification.show("Server verified");
        } else {
            Notification.show("Server is not secure");
        }
    }
}
