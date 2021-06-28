package nl.guuslieben.circle.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;

import java.util.Optional;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.common.Topic;

@Tag("sample-el")
@JsModule("./elements/sample.ts")
public class TopicComponent extends LitTemplate {

    private final ClientService service;
    private final Topic topic;
    @Id
    private H3 name;

    @Id
    private Paragraph author;

    public TopicComponent(ClientService service, Topic topic) {
        this.service = service;
        this.topic = topic;
        this.name.setText(topic.getName());
        this.name.addClickListener(this::onSelect);
        this.author.setText(topic.getAuthor().getName());
    }

    private void onSelect(ClickEvent<H3> event) {
        final long id = this.topic.getId();
        final Optional<Topic> topic = this.service.get("topic/" + id, Topic.class);
        if (topic.isPresent()) {
            Notification.show("Got topic '" + topic.get().getId() + "' with " + topic.get().getResponses().size() + " responses");
        } else {
            Notification.show("Failed to get topic");
        }
    }
}
