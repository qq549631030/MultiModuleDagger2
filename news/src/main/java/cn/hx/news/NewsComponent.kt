package cn.hx.news

import cn.hx.base.AppComponent
import dagger.Component

@NewsScope
@Component(modules = [NewsModule::class], dependencies = [AppComponent::class])
interface NewsComponent {
    fun inject(newsActivity: NewsActivity)
}