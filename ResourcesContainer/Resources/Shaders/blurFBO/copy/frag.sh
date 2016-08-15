uniform sampler2D tex0;
uniform sampler2D tex1;
uniform ivec2 iResolution;
uniform float focalPoint;
uniform float focalScale;

float normpdf(in float x, in float sigma){
	return 0.39894*exp(-0.5*x*x/(sigma*sigma))/sigma;
}


void main() {
    vec2 uv = gl_TexCoord[0].st;
	//ivec2 tSi = textureSize(tex0);


	vec3 c = texture2D(tex0, uv).rgb;
	
	//declare stuff
	const int mSize = 11;
	const int kSize = (mSize-1)/2;
	float kernel[mSize];
	vec3 final_colour = vec3(0.0);
	
	//create the 1-D kernel
	float sigma = 7.0;
	float Z = 0.0;
	for (int j = 0; j <= kSize; ++j)
	{
		kernel[kSize+j] = kernel[kSize-j] = normpdf(float(j), sigma);
	}
	
	//get the normalization factor (as the gaussian has been clamped)
	for (int j = 0; j < mSize; ++j)
	{
		Z += kernel[j];
	}
		
	float blurScale = abs(focalPoint-texture2D(tex1, uv).r) * focalScale;
		
	//read out the texels
	for (int i=-kSize; i <= kSize; ++i)
	{
		for (int j=-kSize; j <= kSize; ++j) {
		
		
			//final_colour += kernel[kSize+j]*kernel[kSize+i]*texture2D(tex0, (uv*tSi+vec2(float(i),float(j))) / tSi).rgb;
			final_colour += kernel[kSize+j]*kernel[kSize+i]*texture2D(tex0, (uv*iResolution + blurScale*vec2(float(i),float(j)))/iResolution ).rgb;

		}
	}
	
	
	gl_FragColor = vec4(final_colour/(Z*Z), 1.0);
}