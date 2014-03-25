Sequel.migration do
  up do
    create_table(:groups) do
      primary_key :id
      String :name, :size => 200
      DateTime :created_at
      DateTime :updated_at

      index [:name], :unique => true, :name => 'groups_unique_name'
    end
  end

  down do
    drop_table(:groups)
  end
end
