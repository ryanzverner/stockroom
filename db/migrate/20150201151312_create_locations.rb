Sequel.migration do
  up do
    create_table(:locations) do
      primary_key :id

      String :name, :size => 200, :null => false
      DateTime :created_at, :null => false
      DateTime :updated_at, :null => false

      index :name, :unique => true, :name => 'locations_unique_name'
    end
  end

  down do
    drop_table(:locations)
  end
end
