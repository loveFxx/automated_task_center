package com.sailvan.dispatchcenter.core.filter;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * GZIP处理Filter
 */
@WebFilter(filterName = "httpServletGzipFilter", urlPatterns = "/")
public class HttpServletGzipFilter implements Filter {
    @Override
    public void destroy() {}
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request), response);
    }
    @Override
    public void init(FilterConfig arg0) throws ServletException {}
}

class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    private HttpServletRequest request;

    public HttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    /**
     * 根据 request header 的 Content-Encoding 判断是否启用 gzip 解压数据流
     * @return
     * @throws IOException
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream stream = request.getInputStream();
        String contentEncoding = request.getHeader("Content-Encoding");
        if (null != contentEncoding && contentEncoding.indexOf("gzip") != -1) {
            try {
                final GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
                ServletInputStream newStream = new ServletInputStream() {
                    @Override
                    public int read() throws IOException {
                        return gzipInputStream.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return false;
                    }

                    @Override
                    public boolean isReady() {
                        return false;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {}
                };
                return newStream;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stream;
    }
}
