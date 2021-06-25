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
      <vaadin-text-field id="name" label="Name"></vaadin-text-field><br>
      <vaadin-text-field id="email" label="Email"></vaadin-text-field><br>
      <vaadin-text-field id="password" label="Password"></vaadin-text-field><br>
      <vaadin-button id="sayHello">Send CSR</vaadin-button><br>
      <p id="key"></p>
    `;
  }
}
