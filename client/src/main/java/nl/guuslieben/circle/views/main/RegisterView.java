package nl.guuslieben.circle.views.main;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.ServerResponse;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.PasswordUtilities;
import nl.guuslieben.circle.views.MainLayout;

@Route(value = "register", layout = MainLayout.class)
@RouteAlias(value = "register", layout = MainLayout.class)
@PageTitle("The Circle - Register")
@Tag("register-view")
@JsModule("./views/register-view.ts")
public class RegisterView extends LitTemplate {

    private final transient ClientService service;

    @Id
    private TextField name;
    @Id
    private TextField email;
    @Id
    private PasswordField password;

    @Id
    private Button register;

    public RegisterView(ClientService service) {
        this.service = service;

        this.name.addValueChangeListener(event -> this.onValidate());
        this.email.addValueChangeListener(event -> this.onValidate());
        this.password.addValueChangeListener(event -> this.onValidate());

        this.onValidate();

        this.register.addClickListener(this::onRegister);
    }

    public void onValidate() {
        boolean enabled = !(this.email.getValue().isEmpty() || this.password.getValue().isEmpty() || this.name.getValue().isEmpty());
        this.register.setEnabled(enabled);
    }

    public void onRegister(ClickEvent<Button> event) {
        Notification.show("Verifying server..");
        final String email = this.email.getValue();
        final UserData data = new UserData(this.name.getValue(), email);
        final Optional<X509Certificate> x509Certificate = this.service.csr(data, this.password.getValue());

        final PublicKey publicKey = this.service.getPair(email).getPublic();

        if (x509Certificate.isPresent()) {
            boolean validCertificate = CertificateUtilities.verify(x509Certificate.get(), this.service.getServerPublic());
            if (!validCertificate) {
                Notification.show("Server is not secure");
                return;
            }
        } else {
            Notification.show("Could not collect certificate from server");
            return;
        }

        Notification.show("Server verified, registering user..");

        final String password = this.password.getValue();

        final User user = new User(
                email,
                this.name.getValue(),
                PasswordUtilities.encrypt(password, password, publicKey)
        );
        final ServerResponse<Boolean> register = this.service.register(user);

        if (register.accepted() && register.getObject()) {
            final String base64private = KeyUtilities.encodeKeyToBase64(this.service.getPair(email).getPrivate());
            final String privateKey = PasswordUtilities.encrypt(base64private, password, publicKey);
            this.service.store(privateKey, email);
            Notification.show(this.name.getValue() + " has been registered!");
        } else {
            Notification.show(register.getMessage());
        }
    }
}
