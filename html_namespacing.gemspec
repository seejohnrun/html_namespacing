# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{html_namespacing}
  s.version = "0.0.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 1.2") if s.respond_to? :required_rubygems_version=
  s.authors = ["Adam Hooper"]
  s.date = %q{2009-03-17}
  s.description = %q{Inserts "class=" attributes within snippets of HTML so CSS and JavaScript can use automatic scopes}
  s.email = %q{adam@adamhooper.com}
  s.extensions = ["ext/html_namespacing/extconf.rb"]
  s.extra_rdoc_files = ["README.rdoc", "ext/html_namespacing/extconf.rb", "ext/html_namespacing/html_namespacing.h", "ext/html_namespacing/html_namespacing.c", "ext/html_namespacing/html_namespacing_ext.c", "lib/html_namespacing.rb", "lib/html_namespacing/plugin/rails.rb"]
  s.files = ["README.rdoc", "Rakefile", "test/c_extension_test.rb", "test/test_helper.rb", "ext/html_namespacing/extconf.rb", "ext/html_namespacing/html_namespacing.h", "ext/html_namespacing/html_namespacing.c", "ext/html_namespacing/html_namespacing_ext.c", "lib/html_namespacing.rb", "lib/html_namespacing/plugin/rails.rb", "Manifest", "html_namespacing.gemspec"]
  s.has_rdoc = true
  s.homepage = %q{http://github.com/adamh/html_namespacing}
  s.rdoc_options = ["--line-numbers", "--inline-source", "--title", "Html_namespacing", "--main", "README.rdoc"]
  s.require_paths = ["lib", "ext"]
  s.rubyforge_project = %q{html_namespacing}
  s.rubygems_version = %q{1.3.1}
  s.summary = %q{Automatic HTML namespacing}
  s.test_files = ["test/c_extension_test.rb", "test/test_helper.rb"]

  if s.respond_to? :specification_version then
    current_version = Gem::Specification::CURRENT_SPECIFICATION_VERSION
    s.specification_version = 2

    if Gem::Version.new(Gem::RubyGemsVersion) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<echoe>, [">= 0"])
    else
      s.add_dependency(%q<echoe>, [">= 0"])
    end
  else
    s.add_dependency(%q<echoe>, [">= 0"])
  end
end