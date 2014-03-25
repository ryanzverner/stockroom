require 'edn'
require 'sequel'
require 'sequel_rake_tasks'

ROOT = File.expand_path('..', __FILE__)

connection_config = if ENV['RDS_HOSTNAME']
                      {
                        :adapter  => 'mysql2',
                        :database => ENV['RDS_DB_NAME'],
                        :host     => ENV['RDS_HOSTNAME'],
                        :port     => ENV['RDS_PORT'],
                        :username => ENV['RDS_USERNAME'],
                        :password => ENV['RDS_PASSWORD']
                      }
                    else
                      env = ENV['stockroom_ENV'] || 'development'
                      jdbc_db_config_filename = File.join(ROOT, 'resources', 'config', "database.clj")
                      jdbc_db_config = EDN.read(File.read(jdbc_db_config_filename))[env.to_sym]

                      {
                        :adapter  => 'mysql2',
                        :database => jdbc_db_config[:db],
                        :host     => jdbc_db_config[:host],
                        :username => jdbc_db_config[:user],
                        :password => jdbc_db_config[:password]
                      }
                    end

connection = Sequel.connect(connection_config)

::Sequel::RakeTasks.new({
  :connection        => connection,
  :connection_config => connection_config,
  :migrator          => ::Sequel::TimestampMigrator,
  :migrations_dir    => File.join(ROOT, 'db', 'migrate'),
  :schema_file       => File.join(ROOT, 'db', 'schema.rb'),
  :structure_file    => File.join(ROOT, 'db', 'seed.sql')
})

namespace :assets do

  desc "Precompile assets"
  task :precompile do
    sh "lein run -m stockroom.task.precompile"
  end

end
