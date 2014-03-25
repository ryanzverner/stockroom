Sequel.migration do
  up do
    create_table(:projects) do
      primary_key :id
      foreign_key :client_id, :clients, :foreign_key_constraint_name => 'projects_fkey_client_id'
      String :name, :size => 200

      DateTime :updated_at
      DateTime :created_at

    end
  end

  down do
    drop_table(:projects)
  end
end
