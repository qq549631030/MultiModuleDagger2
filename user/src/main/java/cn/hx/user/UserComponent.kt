package cn.hx.user

import cn.hx.base.AppComponent
import dagger.Component

@UserScope
@Component(modules = [UserModule::class], dependencies = [AppComponent::class])
interface UserComponent {
    fun inject(userActivity: UserActivity)
}