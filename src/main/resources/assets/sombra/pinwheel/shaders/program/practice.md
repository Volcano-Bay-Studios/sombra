**Parabolic**
```
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
```


**Color Filter**
```
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
```

**DEPTH**
```
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;


void main() {
    vec4 source = texture(DiffuseSampler0,texCoord);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r).rgb/10);
    fragColor = vec4(depth);
}
```

**DARKNESS**
```
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;


void main() {
    vec4 source = texture(DiffuseSampler0,texCoord);
    vec4 white = vec4((source.r+source.g+source.b)/3);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r).rgb/20);
    fragColor = mix(source,vec4(0),clamp(depth,0,1));
}

```

**COLOR REVEAL**
```
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;


void main() {
    vec4 source = texture(DiffuseSampler0,texCoord);
    vec4 white = vec4((source.r+source.g+source.b)/3);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r).rgb/1.5);
    fragColor = mix(source,white,clamp(pow(depth,8),0,1));
}

```

**HELL**
```
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform float powerUniform;

in vec2 texCoord;

out vec4 fragColor;

void make_kernel(inout vec4 n[9], sampler2D tex, vec2 coord)
{
    float w = 1.0 / 1;
    float h = 1.0 / 1;

    n[0] = texture2D(tex, coord + vec2( -w, -h));
    n[1] = texture2D(tex, coord + vec2(0.0, -h));
    n[2] = texture2D(tex, coord + vec2(  w, -h));
    n[3] = texture2D(tex, coord + vec2( -w, 0.0));
    n[4] = texture2D(tex, coord);
    n[5] = texture2D(tex, coord + vec2(  w, 0.0));
    n[6] = texture2D(tex, coord + vec2( -w, h));
    n[7] = texture2D(tex, coord + vec2(0.0, h));
    n[8] = texture2D(tex, coord + vec2(  w, h));
}

void main(void)
{
    vec4 n[9];
    make_kernel( n, DiffuseSampler0, texCoord);

    vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
    vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
    vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));

    vec4 source = texture(DiffuseSampler0,texCoord);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r).rgb/10);

    vec4 sobleOut = vec4( 1.0 - sobel.rgb, 1.0 );

    fragColor = mix(source,sobleOut,depth/10);
}
```

**STRIPES**
```
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
    return mix(value1,value2,seed-value);
}

void main() {
    vec4 source = texture(DiffuseSampler0,texCoord);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r).rgb/20);
    vec4 position = screenToWorldSpace(texCoord,texture(DiffuseDepthSampler,vec2(texCoord)).r);
    fragColor = vec4(noise(position.x*2)*noise(position.z*2)/2);
}
```

**COHESION**
```
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
    return mix(value1,value2,seed-value);
}

void main() {
    vec4 source = texture(DiffuseSampler0,texCoord);
    vec4 white = vec4((source.r+source.g+source.b)/3);
    float depth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,vec2(texCoord.x,0)).r).rgb);
    float sumDepth = 0;
    float average = depth;
    float resolution = 100;
    for (int i = 0 ; i < resolution ; i++) {
        float step = i/resolution;
        float sampledDepth = distance(vec3(0,0,0),screenToLocalSpace(texCoord,texture(DiffuseDepthSampler,vec2(texCoord.x,step)).r).rgb/1000);
        sumDepth = sumDepth + sampledDepth;
        average = average + sampledDepth /2;
    }
    sumDepth = sumDepth/resolution;
    vec4 position = screenToWorldSpace(texCoord,texture(DiffuseDepthSampler,texCoord).r);
    float outputDepth = (1+-sumDepth)-(1+-texCoord.y);
    vec4 outColor = vec4(outputDepth-(average/50));
    fragColor = mix(vec4(outputDepth/2,0,outputDepth,1),vec4(white.r,0,white.r,1),1+-depth);
}
```