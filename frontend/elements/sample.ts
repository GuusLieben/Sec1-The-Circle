import {customElement, html, LitElement} from "lit-element";

@customElement('sample-el')
export class SampleElement extends LitElement {

    render() {
        return html`
    <p id="name"></p>
  `
    }

}
