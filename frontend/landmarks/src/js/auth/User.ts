class User {
    constructor(
        public id: string,
        public username: string,
        public creation: Date,
        public connection: Date,
        public trust: number,
        public avatar: string
) {}
}

export default User;