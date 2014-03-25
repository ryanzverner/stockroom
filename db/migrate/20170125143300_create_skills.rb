Sequel.migration do
  up do
    create_table(:skills) do
      primary_key :id

      String :name
      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:skills)
  end
end
