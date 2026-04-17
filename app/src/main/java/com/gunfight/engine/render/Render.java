package com.gunfight.engine.render;

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
    private float time = 0.0f;

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

            uniform vec3 offset;

            void main() {
                gl_Position = vec4(aPos + offset, 1.0);
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

    public void render() {
        // Update time by 0.01 seconds each frame
        time += 0.01f;
        
        // Use the shader program
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);

        // Set the offset uniform
        // sin(time) → goes between -1 and 1
        float xOffset = (float) Math.sin(time) * 0.5f;

        // Get the location of the offset uniform
        int offsetLocation = glGetUniformLocation(shaderProgram, "offset");

        // Set the offset uniform
        glUniform3f(offsetLocation, xOffset, 0.0f, 0.0f);

        // Draw the triangle
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
