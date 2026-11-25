package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private final Map<String, Integer> uniformCache = new HashMap<>();

    public Shader(String vertexResource, String fragmentResource) {
        String vertSrc = readResource(vertexResource);
        String fragSrc = readResource(fragmentResource);

        int vertId = compileShader(GL_VERTEX_SHADER, vertSrc, vertexResource);
        int fragId = compileShader(GL_FRAGMENT_SHADER, fragSrc, fragmentResource);

        programId = glCreateProgram();
        glAttachShader(programId, vertId);
        glAttachShader(programId, fragId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            throw new RuntimeException("Shader link error (" + vertexResource + ", " + fragmentResource + "):\n" + log);
        }

        glDetachShader(programId, vertId);
        glDetachShader(programId, fragId);
        glDeleteShader(vertId);
        glDeleteShader(fragId);
    }

    private static int compileShader(int type, String src, String name) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(id);
            throw new RuntimeException("Shader compile error in " + name + ":\n" + log);
        }
        return id;
    }

    private static String readResource(String path) {
        InputStream in = Shader.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new RuntimeException("Missing shader resource: " + path);
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading shader: " + path, e);
        }
        return sb.toString();
    }

    public void use() {
        glUseProgram(programId);
    }

    private int loc(String name) {
        Integer cached = uniformCache.get(name);
        if (cached != null) return cached;
        int location = glGetUniformLocation(programId, name);
        uniformCache.put(name, location);
        return location;
    }

    public void setInt(String name, int val) {
        int l = loc(name);
        if (l != -1) glUniform1i(l, val);
    }

    public void setFloat(String name, float val) {
        int l = loc(name);
        if (l != -1) glUniform1f(l, val);
    }

    public void setVec3(String name, float x, float y, float z) {
        int l = loc(name);
        if (l != -1) glUniform3f(l, x, y, z);
    }

    public void setVec3(String name, Vector3f v) {
        setVec3(name, v.x, v.y, v.z);
    }

    public void setVec4(String name, Vector4f v) {
        int l = loc(name);
        if (l != -1) glUniform4f(l, v.x, v.y, v.z, v.w);
    }

    public void setMat4(String name, Matrix4f mat) {
        int l = loc(name);
        if (l != -1) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                mat.get(fb);
                glUniformMatrix4fv(l, false, fb);
            }
        }
    }

    public void dispose() {
        glDeleteProgram(programId);
    }
}
