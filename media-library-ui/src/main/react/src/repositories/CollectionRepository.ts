export default class CollectionRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}

    async collections() {
        let collections = fetch('/collections', {headers: CollectionRepository.headers})
            .then(response => response.json())

        return collections
    }
}

export type Collection = {
    id: string,
    name: string,
}
