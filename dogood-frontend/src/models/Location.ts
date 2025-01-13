type Location = {
    id: number,
    name: number,
    address: AddressTuple
}

type AddressTuple = {
    city: string,
    street: string,
    address: string
}

export default Location;