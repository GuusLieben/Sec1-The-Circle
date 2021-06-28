import '@vaadin/vaadin-text-field';
import { customElement, html, LitElement } from 'lit-element';
import '@vaadin/vaadin-button';

@customElement('topics-view')
export class TopicsView extends LitElement {
    createRenderRoot() {
        // Do not use a shadow root
        return this;
    }

    render() {
        return html`
      <vaadin-text-field id="name" label="Name"></vaadin-text-field><br>
      <vaadin-button id="create">New topic: </vaadin-button>
      <div id="topics"></div>
    `;
    }
}
