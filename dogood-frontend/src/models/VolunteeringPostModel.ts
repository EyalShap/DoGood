import { PostModel } from "./PostModel";

export interface VolunteeringPostModel extends PostModel {
    volunteeringId: number;
    organizationId: number;
    numOfPeopleRequestedToJoin: number;
}

