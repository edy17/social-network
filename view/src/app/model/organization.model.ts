import {Deserializable} from "./deserializable.model";

export class Organization implements Deserializable {

  name: string;
  userIdOfAdmin: string;
  postIds: Array<string>;
  userIdsOfMembers: Array<string>;

  deserialize(input: any): this {
    return Object.assign(this, input);
  }
}
