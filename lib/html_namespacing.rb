module HtmlNamespacing
  autoload(:Plugin, File.dirname(__FILE__) + '/html_namespacing/plugin')

  def self.options
    @options ||= {}
  end
end

if RUBY_PLATFORM == 'java'
  require 'html_namespacing/html_namespacing'
end

# require File.dirname(__FILE__) + '/../ext/html_namespacing/html_namespacing_ext'
