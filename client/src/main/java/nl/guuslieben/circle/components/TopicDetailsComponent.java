package nl.guuslieben.circle.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.ServerResponse;
import nl.guuslieben.circle.common.Response;
import nl.guuslieben.circle.common.Topic;

@Tag("topic-details-component")
@JsModule("./elements/topic-details.ts")
public class TopicDetailsComponent extends LitTemplate {

    @Id
    private H3 name;
    @Id
    private Paragraph author;
    @Id
    private Div responses;
    @Id
    private Div actions;

    private Topic topic;
    private TextField content;
    private Button post;
    private final ClientService service;

    public TopicDetailsComponent(ClientService service) {
        this.service = service;
        this.actions.setVisible(false);
    }

    public void setTopic(Topic topic) {
        this.name.setText(topic.getName());
        this.author.setText("By: " + topic.getAuthor().getName());
        this.responses.removeAll();

        for (Response response : topic.getResponses()) {
            this.responses.add(new ResponseComponent(response));
        }

        this.topic = topic;
        this.actions.setVisible(true);
    }

    public void enableCreating(boolean enable) {
        if (enable) {
            this.content = new TextField("Content");
            this.actions.add(this.content);

            Button create = new Button("Post");
            create.addClickListener(this::onCreate);
            this.actions.add(create);
        }
    }

    private void onCreate(ClickEvent<Button> event) {
        final ServerResponse<Response> response = this.service.createResponse(this.content.getValue(), this.topic.getId());
        if (response.accepted()) {
            Notification.show("Posted response");
            final Optional<Topic> topic = this.service.get("topic/" + this.topic.getId(), Topic.class);
            topic.ifPresent(this::setTopic);
        }
    }

}
