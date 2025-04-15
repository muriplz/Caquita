module.exports = {
    content: ["./src/**/*.{vue,js,html}"],
    theme: {
        extend: {
            fontFamily: {
                sans: ['Minecraftia', 'sans-serif'],
            }
        }
    },
    plugins: [
        require('@tailwindcss/typography')
    ]
}