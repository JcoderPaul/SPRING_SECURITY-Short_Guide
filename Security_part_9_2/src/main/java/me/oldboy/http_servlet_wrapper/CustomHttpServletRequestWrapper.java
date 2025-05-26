package me.oldboy.http_servlet_wrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/*
Создадим класс-оболочку пользовательского запроса. Этот класс позволяет нам создавать и управлять пользовательскими
заголовками, не затрагивая ядро исходного HTTP-запроса. Вот краткое описание того, как это работает:
- Пользовательская оболочка позволяет нам «обернуть» исходный запрос и добавить новые заголовки или изменить
  существующие.
- Расширяя HttpServletRequestWrapper, встроенный класс Java, мы можем переопределить, как обрабатываются заголовки
  в запросе.
- Мы создаем простой метод для добавления заголовков, к которым мы затем можем обращаться, когда они нам понадобятся.
*/

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> customHeaders;

    /*
    Переопределения проведенные ниже гарантируют, что пользовательские заголовки будут
    обрабатываться приложением так же, как и любые другие заголовки, что делает нашу
    пользовательскую оболочку полностью совместимой с существующим кодом.
    */

    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }

    public void addHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    /*
    Этот метод возвращает значение определенного заголовка. Переопределяя его, мы
    позволяем возвращать наши пользовательские значения заголовка при вызове этого
    метода.
    */
    @Override
    public String getHeader(String name) {
        return this.customHeaders.containsKey(name) ?
               this.customHeaders.get(name) :
               super.getHeader(name);
    }

    /*
    Этот метод предоставляет все значения для заголовка (например, заголовки типа Set-Cookie могут иметь
    несколько значений). Переопределяя его, мы гарантируем, что наши пользовательские заголовки, даже с
    несколькими значениями, будут обрабатываться согласованно с HttpServletRequest Java.
    */
    @Override
    public Enumeration<String> getHeaders(String name) {
        return this.customHeaders.containsKey(name) ?
               Collections.enumeration(Collections.singletonList(this.customHeaders.get(name))) :
               super.getHeaders(name);
    }

    /*
    Этот метод возвращает все имена заголовков в перечислении, позволяя другим частям
    приложения видеть наши пользовательские заголовки наряду со встроенными заголовками.
    */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> allHeaders = Collections.list(super.getHeaderNames());
        allHeaders.addAll(this.customHeaders.keySet());
        return Collections.enumeration(allHeaders);
    }
}