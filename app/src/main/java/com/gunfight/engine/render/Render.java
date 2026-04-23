package com.gunfight.engine.render;

import com.gunfight.engine.core.Camera;
import com.gunfight.engine.ecs.Entity;
import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.math.Vector3;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Render {
    private int vbo;
    private int vao;
    private int shaderProgram;
    private float angle = 0.0f;

    /**
     * Initializes OpenGL buffers.
     * Must be called after the OpenGL context is created.
     */
    public void init() {

        // Triangle vertices (x, y, z)
        float[] vertices = {
            0.0f,  0.5f, 0.0f,
           -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
        };

        // Tell the GPU how to read the data (VAO)
        // “Instructions for how to interpret vertex data”
        // "Everything I do next relates to THIS VAO"
        vao = glGenVertexArrays();
        // “Create a container for vertex settings”
        glBindVertexArray(vao);


        // Send the vertices to the GPU (VBO)
        // copy verticies to GPU memory VBO (Vertex Buffer Object)
        // “GPU, give me a chunk of memory”
        vbo = glGenBuffers();              // 1. create buffer on GPU
        // “This is the buffer I’m working with now”
        glBindBuffer(GL_ARRAY_BUFFER, vbo);    // 2. bind it (make it active)
        // “Copy this vertex data into GPU memory”
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); // 3. send data

        // means Attribute 0: has 3 values, type = float, tightly packed
        // “Each vertex = 3 floats (x, y, z)”
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // "Now use this VAO for drawing"
        glBindVertexArray(vao);


        // Vertex Shader Takes your vertex (x, y, z) and decides where it appears on screen
        // aPos = your vertex, "I need vertex data at location 0"
        // gl_Position = where it goes on screen
        String vertexShaderSource = """
            #version 330 core
            layout (location = 0) in vec3 aPos;

            uniform mat4 transform;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                gl_Position = projection * view * transform * vec4(aPos, 1.0);
            }
        """;


        // Fragment Shader Decides the color of each pixel
        // Every pixel = orange color
        String fragmentShaderSource = """
            #version 330 core
            out vec4 FragColor;

            void main() {
                FragColor = vec4(1.0, 0.5, 0.2, 1.0);
            }
        """;


        // Java string → compiled into GPU program
        // Compile shaders
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);


        // Link into program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);


        // Clean up
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    /**
     * Renders the scene using ECS entities and camera for view transformation.
     * Iterates over all entities and draws those with Transform components.
     * @param world the ECS world containing entities and components
     * @param camera the camera to use for view transformation
     */
    public void render(World world, Camera camera) {
        // Update angle - rotate continuously
        angle += 0.02f;

        // Build rotation matrix (shared for all entities)
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        // Extract camera position
        float cx = camera.x;
        float cy = camera.y;
        float cz = camera.z;

        // Extract camera direction vector
        float dx = camera.dirX;
        float dy = camera.dirY;
        float dz = camera.dirZ;

        // Forward vector (copy of direction)
        float fx = dx;
        float fy = dy;
        float fz = dz;

        // Normalize forward vector (make it unit length)
        float flen = (float)Math.sqrt(fx*fx + fy*fy + fz*fz);
        fx /= flen; fy /= flen; fz /= flen;

        // World up vector (pointing up in world space)
        float ux = 0, uy = 1, uz = 0;

        // Calculate right vector using cross product: forward × up
        // Cross product gives a vector perpendicular to both inputs
        float rx = fy*uz - fz*uy;
        float ry = fz*ux - fx*uz;
        float rz = fx*uy - fy*ux;

        // Normalize right vector
        float rlen = (float)Math.sqrt(rx*rx + ry*ry + rz*rz);
        rx /= rlen; ry /= rlen; rz /= rlen;

        // Recompute true up vector using cross product: right × forward
        // This ensures up is perpendicular to both right and forward
        ux = ry*fz - rz*fy;
        uy = rz*fx - rx*fz;
        uz = rx*fy - ry*fx;

        // Build view matrix (rotation + translation)
        // Columns are: right, up, -forward (camera looks opposite to forward)
        // Last column is translation (dot products for position)
        float[] view = {
            rx, ux, -fx, 0,  // Right vector (x, y, z, w)
            ry, uy, -fy, 0,  // Up vector (x, y, z, w)
            rz, uz, -fz, 0,  // Negative forward vector (x, y, z, w)
            -(rx*cx + ry*cy + rz*cz),  // Translation X: -dot(right, cameraPos)
            -(ux*cx + uy*cy + uz*cz),  // Translation Y: -dot(up, cameraPos)
            (fx*cx + fy*cy + fz*cz),   // Translation Z: dot(forward, cameraPos)
            1
        };
        // Build projection matrix (perspective)
        float fov = (float) Math.toRadians(70);
        float aspect = 800.0f / 600.0f;
        float near = 0.1f;
        float far = 100.0f;

        float f = (float) (1.0 / Math.tan(fov / 2));

        float[] projection = {
            f / aspect, 0, 0, 0,
            0, f, 0, 0,
            0, 0, (far + near) / (near - far), -1,
            0, 0, (2 * far * near) / (near - far), 0
        };

        // Use the shader program
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);

        // Send view and projection matrices (shared for all entities)
        int viewLoc = glGetUniformLocation(shaderProgram, "view");
        glUniformMatrix4fv(viewLoc, false, view);

        int projLoc = glGetUniformLocation(shaderProgram, "projection");
        glUniformMatrix4fv(projLoc, false, projection);

        // Iterate over all entities and draw those with Transform components
        for (var entry : world.getEntityData().entrySet()) {
            var components = entry.getValue();

            if (components.containsKey(Transform.class)) {
                Transform t = (Transform) components.get(Transform.class);

                // Build transform matrix per entity (rotation + translation)
                float[] transform = {
                    cos,  sin, 0, 0,
                   -sin,  cos, 0, 0,
                     0,    0,  1, 0,
                    t.x,   t.y, t.z, 1
                };

                int transformLoc = glGetUniformLocation(shaderProgram, "transform");
                glUniformMatrix4fv(transformLoc, false, transform);

                // Draw triangle
                glDrawArrays(GL_TRIANGLES, 0, 3);
            }
        }
    }
}
