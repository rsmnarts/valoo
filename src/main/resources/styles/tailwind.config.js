/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ["./src/main/resources/templates/**/*.html"],
	theme: {
		extend: {
			colors: {
				valorant: {
					base: '#0f1923',
					card: '#1c252e',
					red: '#ff4655',
					text: {
						primary: '#ece8e1',
						secondary: '#768079'
					}
				}
			},
			fontFamily: {
				sans: ['"DIN Next W1G"', 'Arial', 'sans-serif'],
			}
		}
	},
	plugins: [],
}