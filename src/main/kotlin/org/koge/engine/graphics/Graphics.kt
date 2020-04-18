/**
 * Copyright (C) 2020 Moncef YABI
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.koge.engine.graphics

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.koge.engine.graphics.font.Font
import org.koge.game.sprite.Sprite
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30


/**
 *
 * @author Moncef YABI
 */

class Graphics(private val screenWith:Float, private val screenHeight:Float, private val font:Font) {
    private var shader = Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl")

    fun init(){
        shader.create()
    }

    fun drawText(text:String, xPos:Float, yPos:Float, color: Color= Color.WHITE){
        val textHeight = font.getTextHeight(text)
        var drawX: Float = xPos
        var drawY: Float = yPos
        if (textHeight > font.height) {
            drawY += textHeight - font.height
        }
        text.forEach {
            when(it){
                '\n' ->{
                    drawY -= font.height
                    drawX = xPos
                }
                '\r'-> {}
                else -> {
                    val glyph= font.glyphMap[it]!!
                    val model = Model().createFromGlyphTexture(font.texture,glyph,color)
                    draw(model, drawX, drawY)
                    drawX += glyph.width
                    model.clearBuffers()
                }
            }
        }
    }

    fun draw(sprite: Sprite) {
        draw(sprite.model, sprite.position, sprite.scale,sprite.angleOfRotation)
    }

    private fun draw(model: Model, x:Float, y:Float){
        draw(model, Vector2f(x,y))
    }

    private fun draw(model: Model, position: Vector2f =Vector2f(0f, 0f), scale: Vector3f=Vector3f(1f, 1f,1f), angleOfRotation:Float=0f){
        enableVertexArrayAndBindTexture(model)
        begin()
        shader.setUniform(
            shader.getUniformLocation("model"),
            Matrix4f().translate(position.x,position.y,0f)
                .scale(scale)
                .rotate(angleOfRotation, Vector3f(0f,0f,1f))
        )
        shader.setUniform(
            shader.getUniformLocation("projection"),
            Matrix4f().ortho(0f, screenWith,screenHeight,0f,1f,-1f)
        )
        drawElements(model)
        end()
        disableVertexArray()

    }

    private fun drawElements(model: Model) {
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.indices.size, GL11.GL_UNSIGNED_INT, 0)
    }

    private fun begin() {
        shader.bind()
    }

    private fun end(){
        shader.unbind()
    }
    private fun enableVertexArrayAndBindTexture(model: Model) {
        GL30.glBindVertexArray(model.getVAO())
        GL30.glEnableVertexAttribArray(0)
        GL30.glEnableVertexAttribArray(1)
        GL30.glEnableVertexAttribArray(2)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.getIBO())
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL13.glBindTexture(GL11.GL_TEXTURE_2D, model.texture.id)
    }

    private fun disableVertexArray()
    {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
        GL30.glDisableVertexAttribArray(0)
        GL30.glDisableVertexAttribArray(1)
        GL30.glDisableVertexAttribArray(2)
        GL30.glBindVertexArray(0)
    }

    fun destroy(){
        shader.destroy()
    }
}