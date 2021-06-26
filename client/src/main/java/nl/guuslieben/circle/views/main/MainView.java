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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.ServerResponse;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.LoginRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.PasswordUtilities;
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
    private Button register;

    @Id
    private Button login;

    @Id
    private Paragraph key;

    public MainView(ClientService service) {
        this.service = service;

        this.name.addValueChangeListener(event -> this.onValidate());
        this.email.addValueChangeListener(event -> this.onValidate());
        this.password.addValueChangeListener(event -> this.onValidate());

        this.onValidate();

        this.register.addClickListener(this::onRegister);
        this.login.addClickListener(this::onLogin);
    }

    public void onValidate() {
        boolean enabled = !(this.email.getValue().isEmpty() || this.password.getValue().isEmpty());
        this.login.setEnabled(enabled);
        this.register.setEnabled(enabled && !this.name.getValue().isEmpty());
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

            boolean valid = x509Certificate.get().getPublicKey().equals(publicKey);
            if (!valid) {
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

    public void onLogin(ClickEvent<Button> event) {
        final String password = this.password.getValue();
        final String username = this.email.getValue();

        final Optional<X509Certificate> x509Certificate = CertificateUtilities.get(username);
        if (x509Certificate.isEmpty()) {
            Notification.show("No certificate present for user");
            return;
        }

        final X509Certificate certificate = x509Certificate.get();
        final PublicKey publicKey = certificate.getPublicKey();

        final Optional<PrivateKey> privateKey = this.getPrivateKey(username, password, publicKey);
        if (privateKey.isEmpty()) {
            Notification.show("Could not collect keys");
            return;
        }

        KeyPair pair = new KeyPair(publicKey, privateKey.get());
        this.service.setPair(pair);

        final LoginRequest request = new LoginRequest(username, PasswordUtilities.encrypt(password, password, publicKey));
        final ServerResponse<UserData> response = this.service.login(request);

        if (response.accepted()) {
            Notification.show("Welcome, " + response.getObject().getName());
        } else {
            Notification.show(response.getMessage());
        }
    }

    private Optional<PrivateKey> getPrivateKey(String email, String password, Key publicKey) {
        File keys = new File("store/keys");
        final File file = new File(keys, email + ".pfx");
        if (!file.exists()) return Optional.empty();

        try {
            final List<String> lines = Files.readAllLines(file.toPath());
            final String encodedKey = lines.get(0);

            final String decrypted = PasswordUtilities.decrypt(encodedKey, password, publicKey);
            return KeyUtilities.decodeBase64ToPrivate(decrypted);
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }
}
