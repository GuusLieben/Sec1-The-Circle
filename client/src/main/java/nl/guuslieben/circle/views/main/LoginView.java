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
import java.util.function.Consumer;

import lombok.Setter;
import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.ServerResponse;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.LoginRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.PasswordUtilities;
import nl.guuslieben.circle.views.MainLayout;

@Route(value = "login", layout = MainLayout.class)
@RouteAlias(value = "login", layout = MainLayout.class)
@PageTitle("The Circle - Login")
@Tag("login-view")
@JsModule("./views/login-view.ts")
public class LoginView extends LitTemplate {

    private final transient ClientService service;

    @Setter
    private transient Consumer<UserData> afterLogin = ud -> {};

    @Id
    private TextField email;
    @Id
    private PasswordField password;

    @Id
    private Button login;

    public LoginView(ClientService service) {
        this.service = service;
        this.email.addValueChangeListener(event -> this.onValidate());
        this.password.addValueChangeListener(event -> this.onValidate());

        this.onValidate();

        this.login.addClickListener(this::onLogin);
    }

    public void onValidate() {
        boolean enabled = !(this.email.getValue().isEmpty() || this.password.getValue().isEmpty());
        this.login.setEnabled(enabled);
    }

    public void onLogin(ClickEvent<Button> event) {
        final String password = this.password.getValue();
        final String username = this.email.getValue();

        final Optional<X509Certificate> x509Certificate = CertificateUtilities.get(username);
        if (x509Certificate.isEmpty()) {
            Notification.show("Could not verify account");
            return;
        }

        final X509Certificate certificate = x509Certificate.get();
        final PublicKey publicKey = certificate.getPublicKey();

        final Optional<PrivateKey> privateKey = this.getPrivateKey(username, password, publicKey);
        if (privateKey.isEmpty()) {
            Notification.show("Could not verify account");
            return;
        }

        KeyPair pair = new KeyPair(publicKey, privateKey.get());
        this.service.setPair(pair);

        final LoginRequest request = new LoginRequest(username, PasswordUtilities.encrypt(password, password, publicKey));
        final ServerResponse<UserData> response = this.service.login(request);

        if (response.accepted()) {
            Notification.show("Welcome, " + response.getObject().getName());
            this.afterLogin.accept(response.getObject());
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

            if (decrypted == null) return Optional.empty();

            return KeyUtilities.decodeBase64ToPrivate(decrypted);
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }

}
