### 组件化项目

使用dependencies方式

![](images/image1.jpg)

在组件化多模块项目中，app模块只在最终打包的时候直接依赖user,news模块，当user,news作为单独APP来运行时与app不存在依赖关系了，所以Appcomponent不能放在app模块中了这种情况下应该放在base中。

然而如果放在base模块中，base没有user,news的依赖，无法使用注册UserComponent和NewsComponent所以这时候不能再使用SubComponent方法来实现依赖了

这种情况下只能使用dependencies方式

base模块中

```kotlin
@Module
class AppModule {
    @IntoSet
    @Provides
    fun provideString(): String {
        return "app"
    }
}
```

```kotlin
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}
```

```kotlin
class BaseApplication : Application() {
    lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = DaggerAppComponent.factory().create(this)
    }

    companion object {
        lateinit var instance: BaseApplication
    }
}
```

这里AppComponent放在BaseApplication中,这样user,news模块可以直接使用

user模块

```kotlin
@Module
class UserModule {
    @IntoSet
    @Provides
    fun provideString(): String {
        return "user"
    }
}
```

```kotlin
@UserScope
@Component(modules = [UserModule::class], dependencies = [AppComponent::class])
interface UserComponent {
    fun inject(userActivity: UserActivity)
}
```

news模块

```kotlin
@Module
class NewsModule {
    @IntoSet
    @Provides
    fun provideString(): String {
        return "news"
    }
}
```

```kotlin
@NewsScope
@Component(modules = [NewsModule::class], dependencies = [AppComponent::class])
interface NewsComponent {
    fun inject(newsActivity: NewsActivity)
}
```

要生成NewsComponent和UserComponent必须先得到AppComponent,通过以下方法来创建

```kotlin
 val newsComponent = DaggerNewsComponent.builder()
            .appComponent(BaseApplication.instance.appComponent)
            .build()
 val userComponent = DaggerUserComponent.builder()
            .appComponent(BaseApplication.instance.appComponent)
            .build()
```

```kotlin
object NewsComponentHolder {
    val newsComponent: NewsComponent by lazy {
        DaggerNewsComponent.builder()
            .appComponent(BaseApplication.instance.appComponent)
            .build()
    }
}
object UserComponentHolder {
    val userComponent: UserComponent by lazy {
        DaggerUserComponent.builder()
            .appComponent(BaseApplication.instance.appComponent)
            .build()
    }
}
```

最后在Activity中使用和前一篇一样

```kotlin
class NewsActivity : AppCompatActivity() {

    @Inject
    lateinit var set: Set<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        NewsComponentHolder.newsComponent.inject(this)
        text.text = set.toString()
    }
}

class UserActivity : AppCompatActivity() {

    @Inject
    lateinit var set: Set<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        UserComponentHolder.userComponent.inject(this)
        text.text = set.toString()
    }
}
```

