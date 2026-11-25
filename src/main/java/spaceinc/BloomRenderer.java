package spaceinc;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * HDR Framebuffer + Dual-Pass Separable Gaussian Bloom + ACES Filmic Tone Mapping.
 */
public class BloomRenderer {
    private int hdrFbo;
    private int hdrColorTex;
    private int hdrDepthRbo;

    private final int[] pingPongFbo = new int[2];
    private final int[] pingPongTex = new int[2];

    private int width;
    private int height;

    private final Shader brightShader;
    private final Shader blurShader;
    private final Shader compositeShader;
    private final Mesh quad;

    public BloomRenderer(int width, int height, Mesh quad) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.quad = quad;

        this.brightShader = new Shader("shaders/screen.vert", "shaders/bloom_bright.frag");
        this.blurShader = new Shader("shaders/screen.vert", "shaders/bloom_blur.frag");
        this.compositeShader = new Shader("shaders/screen.vert", "shaders/bloom_composite.frag");

        createTargets();
    }

    private void createTargets() {
        hdrFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFbo);

        hdrColorTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hdrColorTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, hdrColorTex, 0);

        hdrDepthRbo = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, hdrDepthRbo);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, hdrDepthRbo);

        int bw = Math.max(1, width / 2);
        int bh = Math.max(1, height / 2);
        for (int i = 0; i < 2; i++) {
            pingPongFbo[i] = glGenFramebuffers();
            pingPongTex[i] = glGenTextures();
            glBindFramebuffer(GL_FRAMEBUFFER, pingPongFbo[i]);
            glBindTexture(GL_TEXTURE_2D, pingPongTex[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, bw, bh, 0, GL_RGBA, GL_FLOAT, 0L);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, pingPongTex[i], 0);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) return;
        if (newWidth == width && newHeight == height) return;
        this.width = newWidth;
        this.height = newHeight;
        deleteTargets();
        createTargets();
    }

    public void beginScene() {
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFbo);
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endSceneAndComposite(float exposure) {
        glDisable(GL_DEPTH_TEST);

        int bw = Math.max(1, width / 2);
        int bh = Math.max(1, height / 2);
        glViewport(0, 0, bw, bh);

        // 1. Bright-pass extract into pingPong[0]
        glBindFramebuffer(GL_FRAMEBUFFER, pingPongFbo[0]);
        brightShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrColorTex);
        brightShader.setInt("uScene", 0);
        brightShader.setFloat("uThreshold", 1.35f);
        quad.draw();

        // 2. Separable Gaussian blur passes
        boolean horizontal = true;
        int passes = 10;
        blurShader.use();
        blurShader.setInt("uImage", 0);
        int srcTex = pingPongTex[0];
        for (int i = 0; i < passes; i++) {
            int targetIdx = horizontal ? 1 : 0;
            glBindFramebuffer(GL_FRAMEBUFFER, pingPongFbo[targetIdx]);
            blurShader.setInt("uHorizontal", horizontal ? 1 : 0);
            glBindTexture(GL_TEXTURE_2D, srcTex);
            quad.draw();
            srcTex = pingPongTex[targetIdx];
            horizontal = !horizontal;
        }

        // 3. Composite HDR + Bloom -> Default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT);

        compositeShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrColorTex);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, srcTex);
        compositeShader.setInt("uScene", 0);
        compositeShader.setInt("uBloom", 1);
        compositeShader.setFloat("uExposure", exposure);
        compositeShader.setFloat("uBloomStrength", 0.22f);
        quad.draw();

        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_DEPTH_TEST);
    }

    private void deleteTargets() {
        glDeleteFramebuffers(hdrFbo);
        glDeleteTextures(hdrColorTex);
        glDeleteRenderbuffers(hdrDepthRbo);
        for (int i = 0; i < 2; i++) {
            glDeleteFramebuffers(pingPongFbo[i]);
            glDeleteTextures(pingPongTex[i]);
        }
    }

    public void dispose() {
        deleteTargets();
        brightShader.dispose();
        blurShader.dispose();
        compositeShader.dispose();
    }
}
