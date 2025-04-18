
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;
float noise(float value) {
    float seed = ceil(value);
    float value1 = (sin(seed+seed*20)+cos(seed-(seed*2)*500)+cos(seed*20)+sin(seed*760)+sin(seed*99))/5;
    float seed2 = floor(value);
    float value2 = (sin(seed2+seed2*20)+cos(seed2-(seed2*2)*500)+cos(seed2*20)+sin(seed2*760)+sin(seed2*99))/5;
    return mix(value1, value2, seed-value);
}

void main() {
    vec4 source = texture(DiffuseSampler0, texCoord);
    vec4 white = vec4((source.r+source.g+source.b)/3);
    float depth = distance(vec3(0, 0, 0), screenToLocalSpace(texCoord, texture(DiffuseDepthSampler, vec2(texCoord.x, 0)).r).rgb)/20;
    float sumDepth = 0;
    float average = depth;
    float resolution = 100;
    for (int i = 0; i < resolution; i++) {
        float step = i/resolution;
        float sampledDepth = distance(vec3(0, 0, 0), screenToLocalSpace(texCoord, texture(DiffuseDepthSampler, vec2(texCoord.x, step)).r).rgb)/1000;
        sumDepth = sumDepth + sampledDepth;
        average = average + sampledDepth /2;
    }
    sumDepth = sumDepth/resolution;
    float outputDepth = (1+-sumDepth)-(1+-texCoord.y);
    vec4 outColor = mix(vec4(outputDepth/2,0,outputDepth,1),vec4(white.r/2,0,white.r,1),depth);
    fragColor = mix(source,outColor,powerUniform);
}