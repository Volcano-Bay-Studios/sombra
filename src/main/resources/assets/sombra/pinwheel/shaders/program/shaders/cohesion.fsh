uniform sampler2D CustomTexture;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    fragColor = texture(CustomTexture, texCoord);
}