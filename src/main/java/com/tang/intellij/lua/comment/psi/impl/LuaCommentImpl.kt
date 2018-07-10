/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.comment.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

/**
 * Created by Tangzx on 2016/11/21.
 *
 */
class LuaCommentImpl(node: ASTNode) : ASTWrapperPsiElement(node), LuaComment {
    override fun <T : LuaDocPsiElement> findTag(t:Class<T>): T? {
        var element: PsiElement? = firstChild
        while (element != null) {
            if (t.isInstance(element)) {
                return t.cast(element)
            }
            element = element.nextSibling
        }
        return null
    }

    override fun <T : LuaDocPsiElement> findTags(t:Class<T>): Collection<T> {
        return PsiTreeUtil.findChildrenOfType(this, t)
    }

    override fun findTags(name: String): Collection<LuaDocTagDef> {
        return PsiTreeUtil.findChildrenOfType(this, LuaDocTagDef::class.java)
    }

    override fun getTokenType(): IElementType {
        return LuaTypes.DOC_COMMENT
    }

    override val owner: LuaCommentOwner?
        get() = LuaCommentUtil.findOwner(this)

    override val moduleName: String?
        get() {
            val classDef = PsiTreeUtil.getChildOfType(this, LuaDocClassDef::class.java)
            if (classDef != null && classDef.module != null) {
                return classDef.name
            }
            return null
        }

    override val isDeprecated: Boolean
        get() = findTags("deprecated").isNotEmpty()

    override fun getParamDef(name: String): LuaDocParamDef? {
        var element: PsiElement? = firstChild
        while (element != null) {
            if (element is LuaDocParamDef) {
                val nameRef = element.paramNameRef
                if (nameRef != null && nameRef.text == name)
                    return element
            }
            element = element.nextSibling
        }
        return null
    }

    override fun getFieldDef(name: String): LuaDocFieldDef? {
        var element: PsiElement? = firstChild
        while (element != null) {
            if (element is LuaDocFieldDef) {
                val nameRef = element.fieldName
                if (nameRef != null && nameRef == name)
                    return element
            }
            element = element.nextSibling
        }
        return null
    }

    override val classDef: LuaDocClassDef?
        get() {
        var element: PsiElement? = firstChild
        while (element != null) {
            if (element is LuaDocClassDef) {
                return element
            }
            element = element.nextSibling
        }
        return null
    }

    override val returnDef: LuaDocReturnDef?
        get() {
            var element: PsiElement? = firstChild
            while (element != null) {
                if (element is LuaDocReturnDef) {
                    return element
                }
                element = element.nextSibling
            }
            return null
        }

    override val typeDef: LuaDocTypeDef?
        get() {
        var element: PsiElement? = firstChild
        while (element != null) {
            if (element is LuaDocTypeDef) {
                return element
            }
            element = element.nextSibling
        }
        return null
    }

    override fun guessType(context: SearchContext): ITy {
        val classDef = classDef
        if (classDef != null)
            return classDef.type
        val typeDef = typeDef
        return typeDef?.type ?: Ty.UNKNOWN
    }

    override fun isOverride(): Boolean {
        var elem = firstChild
        while (elem != null) {
            if (elem is LuaDocTagDef) {
                if (elem.text == "override") return true
            }
            elem = elem.nextSibling
        }
        return false
    }

    override fun createSubstitutor(): ITySubstitutor? {
        if (!LuaSettings.instance.enableGeneric)
            return null

        val list = findTags(LuaDocGenericDef::class.java)
        val map = mutableMapOf<String, String>()
        for (def in list) {
            val name = def.name
            if (name != null) {
                val base = def.classNameRef?.text
                if (base != null) map[name] = base
            }
        }
        return object : TySubstitutor() {
            override fun substitute(clazz: ITyClass): ITy {
                val base = map[clazz.className]
                if (base != null) {
                    return TySerializedClass(clazz.className, clazz.className, base)
                }
                return super.substitute(clazz)
            }
        }
    }

    override fun toString(): String {
        return "DOC_COMMENT"
    }
}
