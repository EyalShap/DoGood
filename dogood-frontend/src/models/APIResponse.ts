interface APIResponse<T> {
    errorString: string,
    error: boolean
    data: T
}

export default APIResponse;