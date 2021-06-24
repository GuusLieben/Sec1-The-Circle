import '@vaadin/vaadin-text-field';
import { customElement, html, LitElement } from 'lit-element';
import '@vaadin/vaadin-button';

@customElement('main-view')
export class MainView extends LitElement {
  createRenderRoot() {
    // Do not use a shadow root
    return this;
  }

  render() {
    return html`
<vaadin-text-field id="name" label="Your name"></vaadin-text-field>
<vaadin-button id="sayHello">
 Say hello
</vaadin-button>
<br>
<div id="list"></div>
`;
  }
}
