Parabolic
uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float power = powerUniform;
    float center = (pow((texCoord.x-.5),2));
    center = 1/pow(center,center*(power*power));
    vec4 source = texture(DiffuseSampler0,vec2(texCoord.x,texCoord.y/center));
    fragColor = source;
}


Color Filter
uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;
in float vertexDistance;

out vec4 fragColor;

float subtractiveDistance(vec4 pos1, vec4 pos2) {
    float r = distance(pos1.r,pos2.r);
    float g = distance(pos1.g,pos2.g);
    float b = distance(pos1.b,pos2.b);
    return abs(r+g+b);
}

void main() {
    vec4 mask = vec4(1,0,0,1);

    vec4 source = texture(DiffuseSampler0,vec2(texCoord.x,texCoord.y));
    vec4 white = vec4((source.r+source.g+source.b)/3);
    float cut = 1+-subtractiveDistance(source, mask);
    if (cut > 0) {
        fragColor = source;
    } else {
        fragColor = vec4(0);
    }
}
