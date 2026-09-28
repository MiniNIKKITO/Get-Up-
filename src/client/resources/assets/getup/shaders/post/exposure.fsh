#version 330

uniform sampler2D InSampler;

layout(std140) uniform ExposureConfig {
    float Exposure;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 source = texture(InSampler, texCoord);
    vec3 rgb = max(source.rgb, vec3(0.000001));
    float multiplier = exp2(Exposure);
    rgb *= multiplier;
    fragColor = vec4(clamp(rgb, 0.0, 1.0), source.a);
}
