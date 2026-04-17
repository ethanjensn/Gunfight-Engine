# ECS Integration and Engine Expansion Plan

This plan implements a simple ECS architecture with query-based systems, polled input with action mapping, first-person camera controls, and multi-object rendering through ECS.

## Phase 1: Core ECS Infrastructure

**Entity System**
- Implement `Entity` as a simple ID wrapper (int or long)
- Add entity generation with unique IDs in World

**Component Storage**
- Implement `World` with component maps: `Map<Class<? extends Component>, Map<EntityID, Component>>`
- Add methods: `createEntity()`, `addComponent(entity, component)`, `getComponent(entity, componentClass)`, `removeComponent(entity, componentClass)`
- Add `removeEntity(entity)` to clean up all components

**Query System (Optimized)**
- Implement query method: `query(Class<? extends Component>... componentTypes)` returns `Set<EntityID>`
- **Optimization**: Start with smallest component map, then filter for entities that have all other required components
- This keeps queries efficient without needing archetypes

## Phase 2: System Architecture

**System Interface**
- Define `System` interface with `update(World world, float deltaTime)` method
- Systems are manually registered with World

**World Integration with Ordering**
- Add `List<System> systems` to World
- Add `registerSystem(System system)` to World
- Enforce system order when registering:
  1. InputSystem
  2. MovementSystem
  3. CombatSystem
  4. RenderSystem
- Add `update(float deltaTime)` that calls all registered systems in order

## Phase 3: Input System

**Input Manager**
- Implement `InputManager` with GLFW polling
- Track key states (pressed, released, held)
- Track mouse position and delta
- Add action mapping: `mapAction(String action, int key)`
- Add input methods:
  - `isActionPressed(String action)` - current frame state
  - `isActionJustPressed(String action)` - single click detection (for shooting)
  - `isActionHeld(String action)` - continuous state (for movement)

**Integration**
- Initialize InputManager in Window.init() with GLFW callbacks
- Call `InputManager.poll()` in Window.update()

## Phase 4: Camera System

**Camera Class (Data Only)**
- Implement `Camera` with position (Vector3), rotation (yaw/pitch)
- Add view matrix generation from camera transform
- Pure data holder, no input logic

**CameraController (Logic)**
- Implement `CameraController` with Camera reference and InputManager
- Add movement methods: `move(Vector3 delta)`, `rotate(float yawDelta, float pitchDelta)`
- First-person controls: WASD for movement, mouse for look
- Update camera based on input in update loop

**Integration**
- Add Camera and CameraController instances to Engine
- Update CameraController in Engine.update() based on input
- Pass Camera to RenderSystem via constructor (dependency injection)

## Phase 5: Transform and Mesh Components

**Transform Component**
- Position (Vector3), rotation (Vector3 for pitch, yaw, roll), scale (Vector3)
- Method to generate 4x4 transform matrix
- Vector3 rotation keeps it future-proof for 3D, even if staying 2D for now

**Mesh Component (Geometry Only)**
- VAO/VBO handles, vertex count
- Load mesh data on creation
- Pure geometry data, no shader reference

**Material Component (Shader + Uniforms)**
- Shader reference
- Uniform values (color, texture, etc.)
- Separation from Mesh allows multiple shaders per mesh

## Phase 6: Render System

**Render System**
- Implement as ECS system with constructor: `RenderSystem(Camera camera)`
- Camera injected via constructor (dependency injection, not global fetch)
- Query entities with Transform + Mesh + Material components
- For each entity: set transform uniform, set view/projection from camera, apply material uniforms, draw mesh

**Refactor Render Class**
- Move shader compilation to Shader class
- Move VAO/VBO management to Mesh class
- Render becomes a system, not a singleton

## Phase 7: Engine Integration

**Engine Updates**
- Add World instance to Engine
- Add InputManager, Camera, and CameraController instances
- Initialize systems in Engine.run()
- Update World and CameraController in Engine.update()

**Explicit Engine Loop Order**
```java
while (running) {
    input.poll();              // 1. Poll input first
    world.update(deltaTime);   // 2. All systems run here (in order)
    renderer.clear();          // 3. Clear buffers
    renderSystem.render();     // 4. Render system draws
    window.swapBuffers();      // 5. Swap buffers
}
```
This separation is critical for correct frame ordering

## Phase 8: Demo Scene

**Create Test Entities**
- Multiple triangles/cubes with different positions
- Register with World
- Verify all render correctly with camera controls

## Implementation Order

1. Core ECS (Entity, World with component maps, queries)
2. System interface and registration
3. InputManager with action mapping
4. Camera class with first-person controls
5. Transform and Mesh components
6. Render system as ECS system
7. Engine integration
8. Demo scene with multiple objects
