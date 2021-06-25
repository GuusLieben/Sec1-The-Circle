package nl.guuslieben.circle.views.main;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.message.CertificateUtilities;
import nl.guuslieben.circle.common.message.Message;
import nl.guuslieben.circle.common.message.MessageUtilities;
import nl.guuslieben.circle.common.rest.RegisterUserModel;
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
    private TextField password;

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

        KeyPair pair = null;
        try {
            pair = CertificateUtilities.generateKeyPair(data);
        }
        catch (NoSuchAlgorithmException e1) {
            Notification.show("Could not prepare keys: " + e1.getMessage());
        }
        
        final String publicKey = MessageUtilities.encodeKeyToBase64(pair.getPublic());
        this.key.setText(publicKey);

        final RegisterUserModel model = new RegisterUserModel(data, this.password.getValue(), publicKey);
        final Optional<Message> response = this.service.send("http://localhost:9090/register", model);

        if (response.isPresent()) {
            final Message message = response.get();
            Notification.show("Got response: " + message.getContent());
        } else {
            Notification.show("No response");
        }
    }
}
