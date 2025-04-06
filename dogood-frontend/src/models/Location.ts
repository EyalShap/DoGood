type Location = {
    id: number,
    name: string,
    address: AddressTuple
}

type AddressTuple = {
    city: string,
    street: string,
    address: string
}

export interface VolunteersToLocation {
    [Key: string]: Location;
}

export default Location;