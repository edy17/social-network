import {Deserializable} from "./deserializable.model";

export class User implements Deserializable {

  id: string;
  username: string;
  email: string;

  deserialize(input: any): this {
    return Object.assign(this, input);
  }
}
