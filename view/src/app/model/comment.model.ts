import {Deserializable} from "./deserializable.model";

export class Comment implements Deserializable {

  id: string;
  instant: string;
  content: string;
  userId: string;
  postId: string;

  deserialize(input: any): this {
    return Object.assign(this, input);
  }
}
